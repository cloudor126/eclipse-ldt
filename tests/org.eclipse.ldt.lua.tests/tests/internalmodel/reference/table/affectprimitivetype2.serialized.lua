do local _={
		unknownglobalvars={

		}
		--[[table: 0x9701540]],
		content={
			localvars={
				[1]={
					item={
						type={
							def={
								shortdescription="",
								name="table",
								fields={
									fieldname={
										type={
											expression={
												sourcerange={
													min=66,
													max=74
												}
												--[[table: 0x93ac238]],
												definition={
													type={
														typename="string",
														tag="primitivetyperef"
													}
													--[[table: 0x94fc728]],
													description="",
													shortdescription="",
													name="fieldname",
													sourcerange={
														min=28,
														max=36
													}
													--[[table: 0x97c7e48]],
													occurrences={
														[1]={
															sourcerange={
																min=28,
																max=36
															}
															--[[table: 0x988edc8]],
															definition=nil --[[ref]],
															tag="MIdentifier"
														}
														--[[table: 0x9754918]],
														[2]=nil --[[ref]]
													}
													--[[table: 0x981a468]],
													tag="item"
												}
												--[[table: 0x970d270]],
												tag="MIdentifier"
											}
											--[[table: 0x93ebfd8]],
											returnposition=1,
											tag="exprtyperef"
										}
										--[[table: 0x97ca810]],
										description="",
										parent=nil --[[ref]],
										shortdescription="",
										name="fieldname",
										sourcerange={
											min=54,
											max=62
										}
										--[[table: 0x9811ca8]],
										occurrences={

										}
										--[[table: 0x952da08]],
										tag="item"
									}
									--[[table: 0x9701650]]
								}
								--[[table: 0x95ba4a0]],
								sourcerange={
									min=0,
									max=0
								}
								--[[table: 0x9811720]],
								description="",
								tag="recordtypedef"
							}
							--[[table: 0x96d24f8]],
							tag="inlinetyperef"
						}
						--[[table: 0x9458bb0]],
						description="",
						shortdescription="",
						name="tablename",
						sourcerange={
							min=7,
							max=15
						}
						--[[table: 0x95a84d8]],
						occurrences={
							[1]={
								sourcerange={
									min=7,
									max=15
								}
								--[[table: 0x9761e08]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x93b53b8]],
							[2]={
								sourcerange={
									min=44,
									max=52
								}
								--[[table: 0x9834d50]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x9706f00]]
						}
						--[[table: 0x94b7458]],
						tag="item"
					}
					--[[table: 0x9882390]],
					scope={
						min=0,
						max=0
					}
					--[[table: 0x96b28c8]]
				}
				--[[table: 0x94915b8]],
				[2]={
					item=nil --[[ref]],
					scope={
						min=0,
						max=0
					}
					--[[table: 0x93ca438]]
				}
				--[[table: 0x952dc18]]
			}
			--[[table: 0x96db278]],
			sourcerange={
				min=1,
				max=10000
			}
			--[[table: 0x96db2a0]],
			content={
				[1]=nil --[[ref]],
				[2]=nil --[[ref]],
				[3]={
					sourcerange={
						min=44,
						max=62
					}
					--[[table: 0x93b8d08]],
					right="fieldname",
					left=nil --[[ref]],
					tag="MIndex"
				}
				--[[table: 0x93b8ce0]],
				[4]=nil --[[ref]]
			}
			--[[table: 0x9841958]],
			tag="MBlock"
		}
		--[[table: 0x9874b10]],
		tag="MInternalContent"
	}
	--[[table: 0x9701518]];
	_.content.localvars[1].item.type.def.fields.fieldname.type.expression.definition.occurrences[1].definition=_.content.localvars[1].item.type.def.fields.fieldname.type.expression.definition;
	_.content.localvars[1].item.type.def.fields.fieldname.type.expression.definition.occurrences[2]=_.content.localvars[1].item.type.def.fields.fieldname.type.expression;
	_.content.localvars[1].item.type.def.fields.fieldname.parent=_.content.localvars[1].item.type.def;
	_.content.localvars[1].item.occurrences[1].definition=_.content.localvars[1].item;
	_.content.localvars[1].item.occurrences[2].definition=_.content.localvars[1].item;
	_.content.localvars[2].item=_.content.localvars[1].item.type.def.fields.fieldname.type.expression.definition;
	_.content.content[1]=_.content.localvars[1].item.occurrences[1];
	_.content.content[2]=_.content.localvars[1].item.type.def.fields.fieldname.type.expression.definition.occurrences[1];
	_.content.content[3].left=_.content.localvars[1].item.occurrences[2];
	_.content.content[4]=_.content.localvars[1].item.type.def.fields.fieldname.type.expression;
	return _;
end