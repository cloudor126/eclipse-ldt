do local _={
		unknownglobalvars={

		}
		--[[table: 0x98d1b88]],
		content={
			localvars={
				[1]={
					item={
						type={
							def={
								shortdescription="",
								name="___",
								fields={
									functionname={
										type={
											def={
												shortdescription="",
												description="",
												name="___",
												returns={

												}
												--[[table: 0x9417ec8]],
												params={

												}
												--[[table: 0x9417ea0]],
												tag="functiontypedef"
											}
											--[[table: 0x95d32c8]],
											tag="inlinetyperef"
										}
										--[[table: 0x9417ef0]],
										description="",
										parent=nil --[[ref]],
										shortdescription="",
										name="functionname",
										sourcerange={
											min=42,
											max=53
										}
										--[[table: 0x947afc8]],
										occurrences={

										}
										--[[table: 0x941c448]],
										tag="item"
									}
									--[[table: 0x947afa0]]
								}
								--[[table: 0x9432050]],
								sourcerange={
									min=0,
									max=0
								}
								--[[table: 0x95d3178]],
								description="",
								tag="recordtypedef"
							}
							--[[table: 0x9431f40]],
							tag="inlinetyperef"
						}
						--[[table: 0x95d31e0]],
						description="",
						shortdescription="",
						name="tablename",
						sourcerange={
							min=7,
							max=15
						}
						--[[table: 0x97b2310]],
						occurrences={
							[1]={
								sourcerange={
									min=7,
									max=15
								}
								--[[table: 0x9392cc0]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x96d0790]],
							[2]={
								sourcerange={
									min=32,
									max=40
								}
								--[[table: 0x98cd680]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x98a4380]]
						}
						--[[table: 0x93e6ea8]],
						tag="item"
					}
					--[[table: 0x97b22e8]],
					scope={
						min=0,
						max=0
					}
					--[[table: 0x93bd570]]
				}
				--[[table: 0x947b070]]
			}
			--[[table: 0x97bbde0]],
			sourcerange={
				min=1,
				max=10000
			}
			--[[table: 0x97bbe08]],
			content={
				[1]=nil --[[ref]],
				[2]={
					sourcerange={
						min=32,
						max=53
					}
					--[[table: 0x98cd748]],
					right="functionname",
					left=nil --[[ref]],
					tag="MIndex"
				}
				--[[table: 0x98cd6a8]],
				[3]={
					localvars={

					}
					--[[table: 0x945c2a8]],
					sourcerange={
						min=54,
						max=72
					}
					--[[table: 0x945c2d0]],
					content={

					}
					--[[table: 0x9798198]],
					tag="MBlock"
				}
				--[[table: 0x97980f8]]
			}
			--[[table: 0x97bbdb8]],
			tag="MBlock"
		}
		--[[table: 0x98d1bb0]],
		tag="MInternalContent"
	}
	--[[table: 0x96ce618]];
	_.content.localvars[1].item.type.def.fields.functionname.parent=_.content.localvars[1].item.type.def;
	_.content.localvars[1].item.occurrences[1].definition=_.content.localvars[1].item;
	_.content.localvars[1].item.occurrences[2].definition=_.content.localvars[1].item;
	_.content.content[1]=_.content.localvars[1].item.occurrences[1];
	_.content.content[2].left=_.content.localvars[1].item.occurrences[2];
	return _;
end